import React from 'react'
import DeleteButton from "./InlineButtonDelete";
import adminService, {Resume} from "../../services/adminService";

type Props = {
	id: string
	setValue: React.Dispatch<React.SetStateAction<Resume[]>>
}

const DeleteButtonResume = (props: Props) => {
	const deleteHandler = () => {
		adminService
			.deleteResume(props.id)
			.then((incomingDeletedResume) => {
				props.setValue((prevResumes: Resume[]) => prevResumes.filter((resume: Resume) => resume.id !== incomingDeletedResume.id))
			})
	}

	return <DeleteButton id={props.id} handleConfirmedDelete={deleteHandler}/>

}

export default DeleteButtonResume
