import React from "react";
import {User} from "../../services/authenticationService";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faEdit} from "@fortawesome/free-solid-svg-icons";
import DeleteButtonUser from "../Buttons/InlineButtonDeleteUser";
import {SelectOption} from "../Selects/SelectStyles";

type Props = {
	user: User
	setUsers: React.Dispatch<React.SetStateAction<User[]>>
	setEditUser: React.Dispatch<React.SetStateAction<string | null>>
	associatedResumeOptions: SelectOption[]
}

function UserListItem(props: Props) {
	return (
		<div className="user" key={props.user.id}>
			<div>{props.user.username}</div>
			<div>{props.user.role}</div>
			<div>{props.associatedResumeOptions.find((option: SelectOption) => option.value === props.user.associatedResume)?.label}</div>
			<div className="action-controls">
				<button className="action-button action-button--cancel" onClick={() => props.setEditUser(props.user.id)}>
					<FontAwesomeIcon icon={faEdit}/>
				</button>
				<DeleteButtonUser id={props.user.id} role={props.user.role} setValue={props.setUsers}/>
			</div>
		</div>
	)
}

export default UserListItem
