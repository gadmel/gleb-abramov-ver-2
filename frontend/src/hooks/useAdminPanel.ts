import {useEffect, useState} from "react";

import {User} from "../services/authenticationService";
import adminService, {Resume} from "../services/adminService";
import {SelectOption} from "../components/Selects/SelectStyles";


function useAdminPanel() {
	const [users, setUsers] = useState<User[]>([])
	const [resumes, setResumes] = useState<Resume[]>([])
	const [associatedResumeOptions, setAssociatedResumeOptions] = useState<SelectOption[]>([])
	const [usersSelectOptions, setUsersSelectOptions] = useState<SelectOption[]>([])

	useEffect(() => {
		adminService
			.getAllUsers()
			.then((incomingUsers: User[]) => {
				setUsers(incomingUsers)
				setUsersSelectOptions(incomingUsers
					.filter((user: User) => !users.map((userInARow: User) => userInARow.associatedResume).includes(user.associatedResume))
					.map((user: User) => {
						return {label: user.username, value: user.id}
					}))
			})
	}, [])

	useEffect(() => {
		adminService
			.getAllResumes()
			.then((incomingResumes: Resume[]) => {
				setResumes(incomingResumes)
				setAssociatedResumeOptions(incomingResumes
					.map((resume: Resume) => {
						return {label: resume.name, value: resume.id}
					}))
			})
	}, [])


	return {users, setUsers, resumes, setResumes, usersSelectOptions, associatedResumeOptions}

}

export default useAdminPanel
