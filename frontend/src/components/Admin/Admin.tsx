import React, {useEffect, useState} from "react";
import {useNavigate} from 'react-router-dom'
import useAuth from "../../hooks/useAuth";
import Layout from "../Layout/Layout";
import LoadingScreen from "../LoadingScreen/LoadingScreen";
import authenticationService, {User} from "../../services/authenticationService";
import adminService from "../../services/adminService";
import DeleteUserButton from "../Buttons/DeleteUserButton";

function Admin() {
	const user = useAuth(true)
	const navigate = useNavigate()

	if (user?.role !== "ADMIN") {
		navigate('/')
	}

	const [users, setUsers] = useState<User[]>([])

	useEffect(() => {
		adminService
			.getAllUsers()
			.then(incomingUsers => {
				setUsers(incomingUsers)
			})
	}, [])

	const handleLogout = () => {
		authenticationService
			.logout()
			.then(() => {
				navigate('/login/')
			})
			.catch(error => {
				console.log(error)
			})
	}

	return (
		<Layout title="Admin control panel">
			{!user
				? <LoadingScreen/>
				: <section id="restricted">
					<div className="full-screen-unit">
						<h2>Admin</h2>
						<h3>Users</h3>
						{users.map(user => {
							return (
								<div className="user" key={user.id}>
									<div>{user.username}</div>
									<div>{user.role}</div>
									<div>{user.associatedResume}</div>
									<DeleteUserButton id={user.id} role={user.role} users={users} setUsers={setUsers}/>
								</div>
							)
						})}
						<button onClick={() => navigate('/secured/register/')}>Register</button>
						<button onClick={handleLogout}>Log out</button>
					</div>
				</section>
			}
		</Layout>
	)
}

export default Admin
