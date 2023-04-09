import React from "react";
import {User} from "../../services/authenticationService";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faEdit} from "@fortawesome/free-solid-svg-icons";
import DeleteButtonUser from "../Buttons/InlineButtonDeleteUser";
import {SelectOptionType} from "../Selects/SelectOption";

type Props = {
	user: User
	setEditUser: React.Dispatch<React.SetStateAction<string | null>>
	refreshData: () => void
	associatedResumeOptions: SelectOptionType[]
}

function UserListItem(props: Props) {
	return (
		<div className="user" key={props.user.id}>
			<div>{props.user.username}</div>
			<div>{props.user.role}</div>
			<div>{props.associatedResumeOptions.find((option: SelectOptionType) => option.value === props.user.associatedResume)?.label}</div>
			<div className="action-controls">
				<button className="action-button action-button--standard" onClick={() => props.setEditUser(props.user.id)}>
					<FontAwesomeIcon icon={faEdit}/>
				</button>
				<DeleteButtonUser id={props.user.id} role={props.user.role} refreshData={props.refreshData}/>
			</div>
		</div>
	)
}

export default UserListItem
