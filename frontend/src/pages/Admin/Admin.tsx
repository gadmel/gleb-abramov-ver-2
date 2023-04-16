import React from "react";
import {useNavigate} from 'react-router-dom'
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBackward, faHouse} from "@fortawesome/free-solid-svg-icons";
import Layout from "../../components/Layout/Layout";
import LoadingScreen from "../../components/LoadingScreen/LoadingScreen";
import authenticationService from "../../services/authenticationService";
import useAuth from "../../hooks/useAuth";
import useAdminPanel from "../../hooks/useAdminPanel";
import ListUsers from "../../components/Lists/ListUsers";
import ListResumes from "../../components/Lists/ListResumes";

function Admin() {
	const user = useAuth(true)
	const navigate = useNavigate()

	if (user?.role !== "ADMIN") {
		navigate('/')
	}

	const {users, resumes, usersSelectOptions, associatedResumeOptions, refreshData} = useAdminPanel()

	const handleLogout = () => {
		authenticationService
			.logout()
			.then(() => navigate('/login/'))
			.catch(error => console.warn(error))
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
						<ListUsers users={users} associatedResumeOptions={associatedResumeOptions} refreshData={refreshData}/>
						<ListResumes resumes={resumes} users={users} usersSelectOptions={usersSelectOptions} refreshData={refreshData}/>
						<button onClick={() => navigate('/secured/register/')}>Register a user</button>
						<button onClick={handleLogout}>Log out</button>
					</div>
				</section>
			}
		</Layout>
	)
}

export default Admin
