import React from "react";
import {useNavigate} from 'react-router-dom'
import axios from 'axios'
import useAuth from "../../hooks/useAuth";
import Layout from "../Layout/Layout";
import LoadingScreen from "../LoadingScreen/LoadingScreen";

function Admin() {
	const user = useAuth(true)
	const navigate = useNavigate()

	if (user !== null && user.role !== "ADMIN") {
		navigate('/')
	}

	const handleLogout = () => {
		axios
			.post('/api/users/logout/')
			.then(() => {
				navigate('/login')
			})
			.catch(error => {
				console.log(error)
			})
	}

	return (
		<Layout title="Admin restricted page">
			{!user
				? <LoadingScreen/>
				: <section id="restricted">
					<div className="full-screen-unit">
						<h2>Admin</h2>
						<button onClick={handleLogout}>Log out</button>
						<button onClick={() => navigate('/secured/register/')}>Register</button>
					</div>
				</section>
			}
		</Layout>
	)
}

export default Admin
