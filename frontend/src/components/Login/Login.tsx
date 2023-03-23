import {ChangeEvent, FormEvent, useState} from "react";
import axios from "axios";
import Layout from "../Layout/Layout";

function Login() {
	const [username, setUsername] = useState("")
	const [password, setPassword] = useState("")

	function handleUsernameChange(event: ChangeEvent<HTMLInputElement>) {
		setUsername(event.target.value)
	}

	function handlePasswordChange(event: ChangeEvent<HTMLInputElement>) {
		setPassword(event.target.value)
	}

	function submitHandler(event: FormEvent<HTMLFormElement>) {
		event.preventDefault()
		console.log(username, password)
		console.log(window.btoa(`${username}:${password}`))
		axios
			.post(
				'/api/users/login/',
				{},
				{
					headers: {
						Authorization: `Basic ${window.btoa(`${username}:${password}`)}`,
					},
				}
			)
			.then(() => {
				setUsername('')
				setPassword('')
			})
			.catch((error) => {
				console.log(error)
			})
	}

	return (
		<Layout title="Login">
			<section id="login">
				<form className="full-screen-unit" onSubmit={submitHandler}>
					<label htmlFor="username">Username</label>
					<input type={"text"} value={username} onChange={handleUsernameChange}/>
					<label htmlFor="password">Password</label>
					<input type={"password"} value={password} onChange={handlePasswordChange}/>
					<button type={"submit"}>Log in</button>
				</form>
			</section>
		</Layout>
	)
}

export default Login