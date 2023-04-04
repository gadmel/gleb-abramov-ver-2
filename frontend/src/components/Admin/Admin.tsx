import React from "react";
import {useNavigate} from 'react-router-dom'
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBackward, faHouse} from "@fortawesome/free-solid-svg-icons";
import Layout from "../Layout/Layout";
import LoadingScreen from "../LoadingScreen/LoadingScreen";
import DeleteButtonResume from "../Buttons/InlineButtonDeleteResume";
import CollapsibleFormCreateResume from "../Forms/CollapsibleFormCreateResume";
import authenticationService from "../../services/authenticationService";
import useAuth from "../../hooks/useAuth";
import useAdminPanel from "../../hooks/useAdminPanel";
import ListUsers from "../Lists/ListUsers";
import {Resume} from "../../services/adminService";

function Admin() {
	const user = useAuth(true)
	const navigate = useNavigate()

	if (user?.role !== "ADMIN") {
		navigate('/')
	}

	const {users, setUsers, resumes, setResumes, usersSelectOptions, associatedResumeOptions, refreshResumes} = useAdminPanel()

	const standardResumeId = "8c687299-9ab7-4f68-8fd9-3de3c521227e"

	const handleLogout = () => {
		authenticationService
			.logout()
			.then(() => {
				navigate('/login/')
			})
			.catch(error => {
				console.warn(error)
			})
	}

	const listAssignedUsersNames = (resume: Resume) => {
		return users.filter(user => resume.userIds?.includes(user.id))
			.map(user => user.username)
			.join(", ")
	}

	return (
		<Layout title="Admin control panel">

			<aside className="overhead left">
				<button className="overhead--button" onClick={() => navigate("/")}>
					<FontAwesomeIcon icon={faHouse} size="2xl"/>
				</button>
			</aside>

			<aside className="overhead right">
				<button className="overhead--button" onClick={() => navigate(-1)}>
					<FontAwesomeIcon icon={faBackward} size="2xl"/>
				</button>
			</aside>

			{!user
				? <LoadingScreen/>
				: <section id="restricted">
					<div className="full-screen-unit">
						<h2>Admin</h2>

						<ListUsers users={users} setUsers={setUsers}
									  associatedResumeOptions={associatedResumeOptions}
									  setResumes={setResumes}/>

						<h3>Resumes</h3>
						{resumes.map((resume: Resume) => {
							return (
								<div className="resume" key={resume.id}>
									<p>{resume.name}</p>
									<p>{listAssignedUsersNames(resume)}</p>
									{resume.id !== standardResumeId &&
                               <DeleteButtonResume id={resume.id} setValue={setResumes} setDependentValue={setUsers}/>}
								</div>
							)
						})}

						<CollapsibleFormCreateResume setResumes={setResumes} usersSelectOptions={usersSelectOptions} setUsers={setUsers} refreshResumes={refreshResumes}/>

						<button onClick={() => navigate('/secured/register/')}>Register a user</button>
						<button onClick={handleLogout}>Log out</button>
					</div>
				</section>
			}
		</Layout>
	)
}

export default Admin
