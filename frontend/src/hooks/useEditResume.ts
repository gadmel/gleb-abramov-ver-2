import {useState, useEffect} from "react";
import {Resume} from "../services/resumeService";
import SelectOption, {SelectOptionType} from "../components/Selects/SelectOption";
import adminService from "../services/adminService";
import {User} from "../services/authenticationService";

type Props = {
	resume: Resume
	editMode: boolean
	setEditMode: (editMode: boolean) => void
}

function useEditResume(props: Props) {
	const {editMode, setEditMode} = props
	const toggleEditMode = () => setEditMode(!editMode)

	const [resume, setResume] = useState<Resume>(props.resume)
	const [usersSelectOptions, setUsersSelectOptions] = useState<SelectOptionType[]>([])

	const isStandardResume = props.resume.id === '8c687299-9ab7-4f68-8fd9-3de3c521227e'

	useEffect(() => {
		adminService
			.getAllUsers()
			.then((incomingUsers: User[]) => {
				setUsersSelectOptions(incomingUsers.map((user: User) => SelectOption.fromUser(user)))
			})
	}, [setEditMode])

	return {resume, setResume, isStandardResume, editMode, toggleEditMode, usersSelectOptions}
}

export default useEditResume
