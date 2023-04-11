import React from 'react'
import DeleteButton from "./InlineButtonDelete";
import adminService from "../../services/adminService";

type Props = {
	id: string
	role: string
	refreshData: () => void
}

const DeleteButtonUser = (props: Props) => {
	const userMayBeDeleted = props.role !== "ADMIN"

	const deleteHandler = () => {
		adminService
			.deleteUser(props.id)
			.then(() => props.refreshData())
	}

	return (
		<>
			{userMayBeDeleted && <DeleteButton id={props.id} role={props.role} handleConfirmedDelete={deleteHandler}/>}
		</>
	)
}

export default DeleteButtonUser
