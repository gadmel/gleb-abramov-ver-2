import React, {useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTrash, faTimes} from "@fortawesome/free-solid-svg-icons";
import adminService from "../../services/adminService";
import {User} from "../../services/authenticationService";

type Props = {
	id: string
	role: string
	users: User[]
	setUsers: (users: User[]) => void
}

const DeleteUserButton = (props: Props) => {

	const [sureToDelete, setSureToDelete] = useState<string | null>(null)

	const handleConfirmedDelete = (id: string) => {
		adminService
			.deleteUser(id)
			.then((deletedUser) => {
				props.setUsers(props.users.filter(user => user.id !== deletedUser.id))
			})
	}

	return (
		<>
			{props.role !== "ADMIN" && (sureToDelete !== props.id
					? <button className="user__action user__action--delete" onClick={() => setSureToDelete(props.id)}>
						<FontAwesomeIcon icon={faTrash}/>
					</button>
					: <>
						<button className="user__action user__action--delete"
								  onClick={() => handleConfirmedDelete(props.id)}>
							<FontAwesomeIcon icon={faTrash}/>
						</button>
						<button className="user__action user__action--cancel" onClick={() => setSureToDelete(null)}>
							<FontAwesomeIcon icon={faTimes}/>
						</button>
					</>
			)}
		</>
	)
}

export default DeleteUserButton
