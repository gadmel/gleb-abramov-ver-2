import React, {useState} from 'react'
import {User} from "../../services/authenticationService";
import UserListItem from "./ListItemUser";
import CollapsibleFormEditUser from "../Forms/CollapsibleFormEditUser";
import {SelectOption} from "../Selects/SelectStyles";
import {Resume} from "../../services/adminService";

type Props = {
	users: User[]
	setUsers: React.Dispatch<React.SetStateAction<User[]>>
	associatedResumeOptions: SelectOption[]
	setResumes: React.Dispatch<React.SetStateAction<Resume[]>>
}

function UsersList(props: Props) {
	const [editUser, setEditUser] = useState<string | null>(null)

	return (
		<>
			<h3>Users</h3>
			{props.users.map((user: User) => {
					return (editUser === user.id)
						? <CollapsibleFormEditUser key={user.id}
															user={user}
															setUsers={props.setUsers}
															setEditUser={setEditUser}
															associatedResumeOptions={props.associatedResumeOptions}
															setResumes={props.setResumes}/>
						: <UserListItem key={user.id}
											 user={user}
											 setUsers={props.setUsers}
											 setEditUser={setEditUser}
											 associatedResumeOptions={props.associatedResumeOptions}/>
				}
			)}
		</>
	)
}

export default UsersList
