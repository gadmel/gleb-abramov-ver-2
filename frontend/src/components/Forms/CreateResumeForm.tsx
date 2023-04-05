import React, {useState} from 'react';
import {useMediaQuery} from 'react-responsive'
import Select from "react-select";
import adminService, {Resume} from "../../services/adminService";
import {selectStyles, selectTheme} from "../Selects/SelectStyles";

type Props = {
	usersSelectOptions: { value: string, label: string }[]
	resumes: Resume[]
	setResumes: React.Dispatch<React.SetStateAction<Resume[]>>
}

function CreateResumeForm(props: Props) {
	const systemPrefersLight = useMediaQuery({query: '(prefers-color-scheme: light)'})

	const [open, setOpen] = useState<boolean>(false)
	const [newResumeName, setNewResumeName] = useState<string>('')
	const [newResumeUserId, setNewResumeUserId] = useState<string>('')

	const handleCreateResume = (event: React.FormEvent<HTMLFormElement>) => {
		event.preventDefault()
		adminService
			.createResume(newResumeName, newResumeUserId)
			.then(resume => {
				props.setResumes([...props.resumes, resume])
			})
	}

	const handleToggleForm = () => {
		setOpen(!open)
	}

	return (
		<form className="form" onSubmit={handleCreateResume}>

			<div className="form__title" onClick={handleToggleForm}>Create resume</div>

			{open && (
				<>
					<div className="form__block">
						<input className="form__input"
								 placeholder="New resume"
								 value={newResumeName}
								 onChange={event => setNewResumeName(event.target.value)}
						/>
						<Select options={props.usersSelectOptions}
								  value={props.usersSelectOptions.find(option => option.value === newResumeUserId)}
								  onChange={option => option !== null && setNewResumeUserId(option.value)}
								  className="user-select"
								  classNamePrefix="user-select"
								  styles={selectStyles}
								  theme={selectTheme(systemPrefersLight ? 'light' : 'dark')}
						/>
					</div>
					<button type="submit" className="form__button">Create</button>
				</>
			)}
		</form>
	)
}

export default CreateResumeForm;
