import {ChangeEvent, FormEvent, useState} from "react";
import {useNavigate} from 'react-router-dom'
import axios from "axios";
import Layout from "../Layout/Layout";
import useAuth from "../../hooks/useAuth";

function Login() {
	const [username, setUsername] = useState("")
	const [password, setPassword] = useState("")

	const user = useAuth()
	const navigate = useNavigate()

	if (!!user && user?.role === "ADMIN") {
		navigate('/admin')
	} else if (!!user && user?.role !== "ADMIN") {
		navigate('/')
	}

	function handleUsernameChange(event: ChangeEvent<HTMLInputElement>) {
		setUsername(event.target.value)
	}

	function handlePasswordChange(event: ChangeEvent<HTMLInputElement>) {
		setPassword(event.target.value)
	}

	function submitHandler(event: FormEvent<HTMLFormElement>) {
		event.preventDefault()
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
				navigate('/')
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