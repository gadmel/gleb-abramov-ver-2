import React, {ChangeEvent, FormEvent, useState} from 'react'
import axios from 'axios'
import {useNavigate} from 'react-router-dom'
import Layout from '../Layout/Layout'
import useAuth from "../../hooks/useAuth";

function Register() {
	const [username, setUsername] = useState('')
	const [password, setPassword] = useState('')

	const user = useAuth()
	const navigate = useNavigate()

	if (!user || user?.role !== "ADMIN") {
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
			.post('/api/users/register/', {
				username,
				password,
			})
			.then(() => {
				setUsername('')
				setPassword('')
			})
			.catch((error) => {
				console.log(error)
			})
	}


	return (
		<Layout title="Register new user">
			<section id="register">
				<form className="full-screen-unit" onSubmit={submitHandler}>
					<label htmlFor="username">Username</label>
					<input type={"text"} value={username} onChange={handleUsernameChange}/>
					<label htmlFor="password">Password</label>
					<input type={"password"} value={password} onChange={handlePasswordChange}/>
					<button type={"submit"}>Register user</button>
				</form>
			</section>
		</Layout>
	)
}

export default Register
