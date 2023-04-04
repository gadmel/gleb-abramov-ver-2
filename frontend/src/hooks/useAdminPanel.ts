import {useEffect, useState} from "react";

import {User} from "../services/authenticationService";
import adminService, {Resume} from "../services/adminService";
import {SelectOption} from "../components/Selects/SelectStyles";


function useAdminPanel() {
	const [users, setUsers] = useState<User[]>([])
	const [resumes, setResumes] = useState<Resume[]>([])
	const [associatedResumeOptions, setAssociatedResumeOptions] = useState<SelectOption[]>([])
	const [usersSelectOptions, setUsersSelectOptions] = useState<SelectOption[]>([])

	const refreshUsers = () => {
		adminService
			.getAllUsers()
			.then((incomingUsers: User[]) => {
				setUsers(incomingUsers)
				setUsersSelectOptions(incomingUsers
					.map((user: User) => {
						return {label: user.username, value: user.id}
					}))
			})
	}

	const refreshResumes = () => {
		adminService
			.getAllResumes()
			.then((incomingResumes: Resume[]) => {
				setResumes(incomingResumes)
				setAssociatedResumeOptions(incomingResumes
					.map((resume: Resume) => {
						return {label: resume.name, value: resume.id}
					}))
			})
	}

	useEffect(() => {
		refreshUsers()
	}, [])

	useEffect(() => {
		refreshResumes()
	}, [])


	return {
		users,
		setUsers,
		resumes,
		setResumes,
		usersSelectOptions,
		associatedResumeOptions,
		refreshUsers,
		refreshResumes
	}
}

export default useAdminPanel
