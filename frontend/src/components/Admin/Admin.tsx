import React from "react";
import {useNavigate} from 'react-router-dom'
import Layout from "../Layout/Layout";
import LoadingScreen from "../LoadingScreen/LoadingScreen";
import DeleteButtonUser from "../Buttons/InlineButtonDeleteUser";
import DeleteButtonResume from "../Buttons/InlineButtonDeleteResume";
import CreateResumeForm from "../Forms/CreateResumeForm";
import authenticationService from "../../services/authenticationService";
import useAuth from "../../hooks/useAuth";
import useAdminPanel from "../../hooks/useAdminPanel";

function Admin() {
	const user = useAuth(true)
	const navigate = useNavigate()

	if (user?.role !== "ADMIN") {
		navigate('/')
	}

	const {users, setUsers, resumes, setResumes, usersSelectOptions} = useAdminPanel()

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
									<DeleteButtonUser id={user.id} role={user.role} value={users} setValue={setUsers}/>
								</div>
							)
						})}

						<h3>Resumes</h3>
						{resumes.map(resume => {
							return (
								<div className="resume" key={resume.id}>
									<p>{resume.name}</p>
									<p>{users.find(user => user.id === resume.userId)?.username}</p>
									<DeleteButtonResume id={resume.id} value={resumes} setValue={setResumes}/>
								</div>
							)
						})}

						<CreateResumeForm resumes={resumes} setResumes={setResumes} usersSelectOptions={usersSelectOptions}/>

						<button onClick={() => navigate('/secured/register/')}>Register a user</button>
						<button onClick={handleLogout}>Log out</button>
					</div>
				</section>
			}
		</Layout>
	)
}

export default Admin
