import React, {ChangeEvent, FormEvent, useState} from 'react'
import {useNavigate} from 'react-router-dom'
import Layout from '../../components/Layout/Layout'
import useAuth from "../../hooks/useAuth";
import adminService from "../../services/adminService";
import LoadingScreen from "../../components/LoadingScreen/LoadingScreen";
import OverheadButtonBack from "../../components/Navigation/OverheadButtonBack";

function Register() {
	const [username, setUsername] = useState('')
	const [password, setPassword] = useState('')

	const user = useAuth(true)
	const navigate = useNavigate()

	if (user?.role !== "ADMIN") {
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
		adminService
			.registerUser(username, password)
			.then(() => {
				setUsername('')
				setPassword('')
			})
			.catch((error) => {
				console.warn(error)
			})
	}


	return (
		<Layout title="Register new user">
			<OverheadButtonBack/>
			{!user
				? <LoadingScreen/>
				: <section id="register">
					<form className="full-screen-unit" onSubmit={submitHandler}>
						<label htmlFor="username">Username</label>
						<input type={"text"} value={username} onChange={handleUsernameChange}/>
						<label htmlFor="password">Password</label>
						<input type={"password"} value={password} onChange={handlePasswordChange}/>
						<button type={"submit"}>Register user</button>
					</form>
				</section>}
		</Layout>
	)
}

export default Register
