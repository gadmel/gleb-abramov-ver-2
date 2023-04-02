import React, {useState} from 'react'
import {useMediaQuery} from "react-responsive";
import Select from "react-select";
import CollapsibleForm from "./CollapsibleForm";
import adminService, {Resume} from "../../services/adminService";
import {User} from "../../services/authenticationService";
import {SelectOption, selectStyles, selectTheme} from "../Selects/SelectStyles";

type Props = {
	user: User
	setUsers: React.Dispatch<React.SetStateAction<User[]>>
	setEditUser: React.Dispatch<React.SetStateAction<string | null>>
	associatedResumeOptions: SelectOption[]
	setResumes: React.Dispatch<React.SetStateAction<Resume[]>>
}

function CollapsibleFormEditUser(props: Props) {
	const systemPrefersLight = useMediaQuery({query: '(prefers-color-scheme: light)'})

	const [updatedUsername, setUpdatedUsername] = useState<string>(props.user.username)
	const [updatedAssociatedResume, setUpdatedAssociatedResume] = useState<string>(props.user.associatedResume)

	const handleUpdateUser = (event: React.FormEvent<HTMLFormElement>) => {
		event.preventDefault()
		adminService
			.updateUser(props.user.id, updatedUsername, updatedAssociatedResume)
			.then((incomingUpdatedUser) => {
				props.setUsers(prevUsers => prevUsers.map((user: User) =>
					user.id === incomingUpdatedUser.id
						? incomingUpdatedUser
						: user))

			})
		props.setResumes(prevResumes => prevResumes.map((resume: Resume) =>
			resume.userId === props.user.id
				? {...resume, userId: updatedAssociatedResume}
				: resume))
		props.setEditUser(null)

	}

	return (
		<CollapsibleForm formTitle={"Edit user '" + props.user.username + "'"}
							  formActionName="Save changes"
							  handleSubmit={handleUpdateUser}
							  handleCancel={() => props.setEditUser(null)}
							  expandedAndFixed={true}
		>
			<input className="form__input"
					 value={updatedUsername}
					 onChange={event => setUpdatedUsername(event.target.value)}
			/>
			<Select options={props.associatedResumeOptions}
					  value={props.associatedResumeOptions.find(option => option.value === updatedAssociatedResume) || null}
					  onChange={option => option !== null && setUpdatedAssociatedResume(option.value)}
					  className="resume-select"
					  classNamePrefix="resume-select"
					  styles={selectStyles}
					  theme={selectTheme(systemPrefersLight ? 'light' : 'dark')}
			/>

		</CollapsibleForm>
	)
}

export default CollapsibleFormEditUser
