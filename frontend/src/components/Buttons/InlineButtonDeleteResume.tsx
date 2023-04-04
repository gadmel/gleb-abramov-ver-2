import React from 'react'
import DeleteButton from "./InlineButtonDelete";
import adminService, {} from "../../services/adminService";

type Props = {
	id: string
	refreshData: () => void
}

const DeleteButtonResume = (props: Props) => {

	const deleteHandler = () => {
		adminService
			.deleteResume(props.id)
			.then(() => props.refreshData())
	}

	return <DeleteButton id={props.id} handleConfirmedDelete={deleteHandler}/>

}

export default DeleteButtonResume
