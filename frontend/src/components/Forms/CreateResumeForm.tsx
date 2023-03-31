import React, {useState} from 'react';
import {useMediaQuery} from 'react-responsive'
import Select from "react-select";
import adminService from "../../services/adminService";
import {selectStyles, selectTheme} from "../Selects/SelectStyles";

type Props = {
	usersSelectOptions: { value: string, label: string }[]
}

function CreateResumeForm(props: Props) {
	const systemPrefersLight = useMediaQuery({query: '(prefers-color-scheme: light)'})

	const [newResumeName, setNewResumeName] = useState<string>('')
	const [newResumeUserId, setNewResumeUserId] = useState<string>('')

	const handleCreateResume = () => {
		adminService.createResume(newResumeName, newResumeUserId)
	}


	return <div className="form">
		<label>Create resume</label>
		<div className="row">
			<input className="form__input" placeholder="New resume" value={newResumeName}
					 onChange={event => setNewResumeName(event.target.value)}/>
			<Select options={props.usersSelectOptions}
					  value={props.usersSelectOptions.find(option => option.value === newResumeUserId)}
					  onChange={option => option !== null && setNewResumeUserId(option.value)}
					  className="user-select"
					  classNamePrefix="user-select"
					  styles={selectStyles}
					  theme={selectTheme(systemPrefersLight ? 'light' : 'dark')}/>
		</div>
		<button onClick={handleCreateResume} className="form__button">Create</button>
	</div>

}

export default CreateResumeForm;
