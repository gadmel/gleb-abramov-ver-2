import {ChangeEvent, FormEvent, useState} from "react";
import {useNavigate} from 'react-router-dom'

import Layout from "../Layout/Layout";
import authenticationService from "../../services/authenticationService";

function Login() {
	const [username, setUsername] = useState("")
	const [password, setPassword] = useState("")

	const navigate = useNavigate()

	function handleUsernameChange(event: ChangeEvent<HTMLInputElement>) {
		setUsername(event.target.value)
	}

	function handlePasswordChange(event: ChangeEvent<HTMLInputElement>) {
		setPassword(event.target.value)
	}

	function submitHandler(event: FormEvent<HTMLFormElement>) {
		event.preventDefault()
		authenticationService
			.login(username, password)
			.then(() => {
				setUsername('')
				setPassword('')
				navigate('/')
			})
			.catch((error: Error) => {
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
