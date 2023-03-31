import React from 'react'
import DeleteButton from "./InlineButtonDelete";
import {User} from "../../services/authenticationService";
import adminService from "../../services/adminService";

type Props = {
	id: string
	role: string
	value: User[]
	setValue: React.Dispatch<React.SetStateAction<User[]>>
}

const DeleteButtonUser = (props: Props) => {
	const userMayBeDeleted = props.role !== "ADMIN"

	const deleteHandler = () => {
		adminService
			.deleteUser(props.id)
			.then((incomingDeletedUser) => {
				props.setValue(props.value.filter((user: User) => user.id !== incomingDeletedUser.id))
			})
	}

	return (
		<>
			{userMayBeDeleted && <DeleteButton id={props.id} role={props.role} handleConfirmedDelete={deleteHandler}/>}
		</>
	)
}

export default DeleteButtonUser
