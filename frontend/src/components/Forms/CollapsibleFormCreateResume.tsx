import React, {useState} from 'react';
import {useMediaQuery} from 'react-responsive'
import Select from "react-select";
import adminService from "../../services/adminService";
import CollapsibleForm from "./CollapsibleForm";
import {SelectOptionType} from "../Selects/SelectOption";
import {selectStyles, selectTheme} from "../Selects/SelectStyles";

type Props = {
	usersSelectOptions: SelectOptionType[]
	refreshData: () => void
}

function CollapsibleFormCreateResume(props: Props) {
	const systemPrefersLight = useMediaQuery({query: '(prefers-color-scheme: light)'})

	const [newResumeName, setNewResumeName] = useState<string>('')
	const [newAddressing, setNewAddressing] = useState<string>('')
	const [newResumeUserIds, setNewResumeUserIds] = useState<string[]>([])

	const handleCreateResume = (event: React.FormEvent<HTMLFormElement>) => {
		event.preventDefault()
		adminService
			.createResume(newResumeName, newAddressing, newResumeUserIds)
			.then(() => props.refreshData())
			.finally(() => {
				setNewResumeName('')
				setNewResumeUserIds([])
			})
	}

	return (
		<CollapsibleForm formTitle="Create resume" formActionName="Create" handleSubmit={handleCreateResume}>
			<input className="form__input"
					 placeholder="New resume name"
					 value={newResumeName}
					 onChange={event => setNewResumeName(event.target.value)}
			/>
			<textarea
				rows={8}
				className="form__input"
				placeholder="Addressing"
				value={newAddressing}
				onChange={event => setNewAddressing(event.target.value)}
			/>
			<Select isMulti
					  options={props.usersSelectOptions}
					  value={props.usersSelectOptions.filter((option: SelectOptionType) => newResumeUserIds.includes(option.value)) || null}
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
