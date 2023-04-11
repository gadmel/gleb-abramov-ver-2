import React, {useState} from 'react'
import {User} from "../../services/authenticationService";
import UserListItem from "./ListItemUser";
import CollapsibleFormEditUser from "../Forms/CollapsibleFormEditUser";
import {SelectOptionType} from "../Selects/SelectOption";

type Props = {
	users: User[]
	associatedResumeOptions: SelectOptionType[]
	refreshData: () => void
}

function UsersList(props: Props) {
	const [editUser, setEditUser] = useState<string | null>(null)

	return (
		<>
			<h3>Users</h3>
			{props.users.map((user: User) => {
					return (editUser === user.id)
						? <CollapsibleFormEditUser key={user.id}
															user={user} setEditUser={setEditUser}
															refreshData={props.refreshData}
															associatedResumeOptions={props.associatedResumeOptions}/>
						: <UserListItem key={user.id}
											 user={user} setEditUser={setEditUser}
											 refreshData={props.refreshData}
											 associatedResumeOptions={props.associatedResumeOptions}/>
				}
			)}
		</>
	)
}

export default UsersList
