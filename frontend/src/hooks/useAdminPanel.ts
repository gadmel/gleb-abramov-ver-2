import {useEffect, useState} from "react";

import {User} from "../services/authenticationService";
import adminService, {Resume} from "../services/adminService";


function useAdminPanel() {
	const [users, setUsers] = useState<User[]>([])
	const [resumes, setResumes] = useState<Resume[]>([])

	useEffect(() => {
		adminService
			.getAllUsers()
			.then(incomingUsers => {
				setUsers(incomingUsers)
			})
	}, [])

	useEffect(() => {
		adminService
			.getAllResumes()
			.then(incomingResumes => {
				setResumes(incomingResumes)
			})
	}, [])


	const usersWithoutResume = users.filter(user => user.associatedResume !== null || user.associatedResume !== "[]")
	const usersSelectOptions = [...usersWithoutResume.map((user: User) => {
		return {label: user.username, value: user.id}
	})]


	return {users, setUsers, resumes, setResumes, usersSelectOptions}

}

export default useAdminPanel
