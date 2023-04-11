import {useEffect, useState} from "react";
import {User} from "../services/authenticationService";
import adminService, {Resume} from "../services/adminService";
import SelectOption, {SelectOptionType} from "../components/Selects/SelectOption";


function useAdminPanel() {
	const [users, setUsers] = useState<User[]>([])
	const [resumes, setResumes] = useState<Resume[]>([])
	const [associatedResumeOptions, setAssociatedResumeOptions] = useState<SelectOptionType[]>([])
	const [usersSelectOptions, setUsersSelectOptions] = useState<SelectOptionType[]>([])

	useEffect(() => {
		refreshData()
	}, [])

	const refreshUsers = () => {
		adminService
			.getAllUsers()
			.then((incomingUsers: User[]) => {
				setUsers(incomingUsers)
				setUsersSelectOptions(incomingUsers.map(
					(user: User) => SelectOption.fromUser(user)))
			})
	}

	const refreshResumes = () => {
		adminService
			.getAllResumes()
			.then((incomingResumes: Resume[]) => {
				setResumes(incomingResumes)
				setAssociatedResumeOptions(incomingResumes.map(
					(resume: Resume) => SelectOption.fromResume(resume)))
			})
	}

	const refreshData = () => {
		refreshUsers()
		refreshResumes()
	}

	return {users, resumes, usersSelectOptions, associatedResumeOptions, refreshData}
}

export default useAdminPanel
