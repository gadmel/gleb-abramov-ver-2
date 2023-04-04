import React, {useState} from 'react';
import {useMediaQuery} from 'react-responsive'
import Select from "react-select";
import adminService, {Resume} from "../../services/adminService";
import CollapsibleForm from "./CollapsibleForm";
import {SelectOption, selectStyles, selectTheme} from "../Selects/SelectStyles";
import {User} from "../../services/authenticationService";

type Props = {
	usersSelectOptions: SelectOption[]
	setResumes: React.Dispatch<React.SetStateAction<Resume[]>>
	setUsers: React.Dispatch<React.SetStateAction<User[]>>
	refreshResumes: () => void
}

function CollapsibleFormCreateResume(props: Props) {
	const systemPrefersLight = useMediaQuery({query: '(prefers-color-scheme: light)'})

	const [newResumeName, setNewResumeName] = useState<string>('')
	const [newResumeUserIds, setNewResumeUserIds] = useState<string[]>([])

	const handleCreateResume = (event: React.FormEvent<HTMLFormElement>) => {
		event.preventDefault()
		adminService
			.createResume(newResumeName, newResumeUserIds)
			.then((incomingCreatedResume: Resume) => {
				props.refreshResumes()
				props.setUsers((prevUsers: User[]) => prevUsers.map((user: User) =>
					newResumeUserIds.includes(user.id)
						? {...user, associatedResume: incomingCreatedResume.id}
						: user
				))
			})
			.finally(() => {
				setNewResumeName('')
				setNewResumeUserIds([])
			})
	}

	return (
		<CollapsibleForm formTitle="Create resume" formActionName="Create" handleSubmit={handleCreateResume}>
			<input className="form__input"
					 placeholder="New resume"
					 value={newResumeName}
					 onChange={event => setNewResumeName(event.target.value)}
			/>
			<Select isMulti
					  options={props.usersSelectOptions}
					  value={props.usersSelectOptions.filter(option => newResumeUserIds.includes(option.value)) || null}
					  onChange={options => setNewResumeUserIds(options.map(option => option.value))}
					  className="user-select"
					  classNamePrefix="user-select"
					  styles={selectStyles}
					  theme={selectTheme(systemPrefersLight ? 'light' : 'dark')}
			/>
		</CollapsibleForm>
	)
}

export default CollapsibleFormCreateResume;
