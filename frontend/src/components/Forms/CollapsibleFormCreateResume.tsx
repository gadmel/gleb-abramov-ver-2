import React, {useState} from 'react';
import {useMediaQuery} from 'react-responsive'
import Select from "react-select";
import adminService, {Resume} from "../../services/adminService";
import CollapsibleForm from "./CollapsibleForm";
import {SelectOption, selectStyles, selectTheme} from "../Selects/SelectStyles";

type Props = {
	usersSelectOptions: SelectOption[]
	setResumes: React.Dispatch<React.SetStateAction<Resume[]>>
}

function CollapsibleFormCreateResume(props: Props) {
	const systemPrefersLight = useMediaQuery({query: '(prefers-color-scheme: light)'})

	const [newResumeName, setNewResumeName] = useState<string>('')
	const [newResumeUserId, setNewResumeUserId] = useState<string>('')

	const handleCreateResume = (event: React.FormEvent<HTMLFormElement>) => {
		event.preventDefault()
		adminService
			.createResume(newResumeName, newResumeUserId)
			.then(resume => {
				props.setResumes((prevResumes: Resume[]) => [...prevResumes, resume])
			})
			.finally(() => {
				setNewResumeName('')
				setNewResumeUserId('')
			})
	}

	return (
		<CollapsibleForm formTitle="Create resume" formActionName="Create" handleSubmit={handleCreateResume}>
			<input className="form__input"
					 placeholder="New resume"
					 value={newResumeName}
					 onChange={event => setNewResumeName(event.target.value)}
			/>
			<Select options={props.usersSelectOptions}
					  value={props.usersSelectOptions.find(option => option.value === newResumeUserId) || null}
					  onChange={option => option !== null && setNewResumeUserId(option.value)}
					  className="user-select"
					  classNamePrefix="user-select"
					  styles={selectStyles}
					  theme={selectTheme(systemPrefersLight ? 'light' : 'dark')}
			/>
		</CollapsibleForm>
	)
}

export default CollapsibleFormCreateResume;
