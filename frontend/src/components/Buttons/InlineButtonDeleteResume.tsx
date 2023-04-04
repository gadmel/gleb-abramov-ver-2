import React from 'react'
import DeleteButton from "./InlineButtonDelete";
import adminService, {Resume} from "../../services/adminService";
import {User} from "../../services/authenticationService";

type Props = {
	id: string
	setValue: React.Dispatch<React.SetStateAction<Resume[]>>
	setDependentValue?: React.Dispatch<React.SetStateAction<User[]>>
}

const DeleteButtonResume = (props: Props) => {
	const standardResumeId = "8c687299-9ab7-4f68-8fd9-3de3c521227e"

	const deleteHandler = () => {
		adminService
			.deleteResume(props.id)
			.then((incomingDeletedResume) => {
				props.setValue((prevResumes: Resume[]) => prevResumes.filter((resume: Resume) => resume.id !== incomingDeletedResume.id))
				!!props.setDependentValue && props.setDependentValue((prevUsers: User[]) => prevUsers
					.map((user: User) =>
						user.associatedResume === incomingDeletedResume.id
							? {...user, associatedResume: standardResumeId}
							: user))
			})
	}

	return <DeleteButton id={props.id} handleConfirmedDelete={deleteHandler}/>

}

export default DeleteButtonResume
