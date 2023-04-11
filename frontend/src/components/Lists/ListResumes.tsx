import {Resume} from "../../services/adminService";
import DeleteButtonResume from "../Buttons/InlineButtonDeleteResume";
import {User} from "../../services/authenticationService";
import CollapsibleFormCreateResume from "../Forms/CollapsibleFormCreateResume";
import {SelectOptionType} from "../Selects/SelectOption";

type Props = {
	resumes: Resume[]
	users: User[]
	refreshData: () => void
	usersSelectOptions: SelectOptionType[]
}

function ListResumes(props: Props) {
	const standardResumeId = "8c687299-9ab7-4f68-8fd9-3de3c521227e"

	const listAssignedUsersNames = (resume: Resume) => {
		return props.users.filter((user: User) => resume.userIds?.includes(user.id))
			.map((user: User) => user.username)
			.join(", ")
	}

	return (
		<>
			<h3>Resumes</h3>
			{props.resumes.map((resume: Resume) => (
					<div className="resume" key={resume.id}>
						<p>{resume.name}</p>
						<p>{listAssignedUsersNames(resume)}</p>
						{resume.id !== standardResumeId &&
                      <DeleteButtonResume id={resume.id} refreshData={props.refreshData}/>}
					</div>
				)
			)}

			<CollapsibleFormCreateResume usersSelectOptions={props.usersSelectOptions} refreshData={props.refreshData}/>
		</>
	)
}

export default ListResumes
