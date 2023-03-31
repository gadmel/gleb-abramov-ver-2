import React from "react";
import {useNavigate} from 'react-router-dom'
import useAuth from "../../hooks/useAuth";
import Layout from "../Layout/Layout";
import LoadingScreen from "../LoadingScreen/LoadingScreen";
import authenticationService from "../../services/authenticationService";
import DeleteUserButton from "../Buttons/DeleteUserButton";
import useAdminPanel from "../../hooks/useAdminPanel";
import CreateResumeForm from "../Forms/CreateResumeForm";

function Admin() {
	const user = useAuth(true)
	const navigate = useNavigate()

	if (user?.role !== "ADMIN") {
		navigate('/')
	}

	const {users, setUsers, usersSelectOptions} = useAdminPanel()

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

						<h3>Resumes</h3>


						<CreateResumeForm usersSelectOptions={usersSelectOptions}/>

						<button onClick={() => navigate('/secured/register/')}>Register a user</button>
						<button onClick={handleLogout}>Log out</button>
					</div>
				</section>
			}
		</Layout>
	)
}

export default Admin
