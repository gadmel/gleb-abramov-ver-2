import React, {useState, useEffect} from "react";
import {Resume} from "../../services/resumeService";
import CollapsibleFormEditResume from "../../components/Forms/CollapsibleFormEditResume";
import ViewResumePage from "../../components/Resume/ViewResumePage";
import useEditResume from "../../hooks/useEditResume";
import OverheadButtonHome from "../../components/Navigation/OverheadButtonHome";
import OverheadButtonBack from "../../components/Navigation/OverheadButtonBack";

type Props = {
	resume: Resume
	editMode: boolean
	setEditMode: (editMode: boolean) => void
	children?: React.ReactNode
}

function EditResumePage(props: Props) {
	const {resume, editMode, isStandardResume, toggleEditMode, usersSelectOptions} = useEditResume(props)

	const [name, setName] = useState<string>(props.resume.name || "")
	const [addressing, setAddressing] = useState<string>(props.resume.addressing || "")
	const [userIds, setUserIds] = useState<string[]>(props.resume.userIds || [])

	const [preview, setPreview] = useState<Resume>({} as Resume)

	useEffect(() => {
		setPreview({...props.resume, name, userIds} as Resume)
	}, [editMode])

	return (
		<>
			<OverheadButtonHome/>
			<OverheadButtonBack/>
			<aside className="overhead center">
				<button onClick={toggleEditMode}>Toggle Edit Mode</button>
			</aside>

			{editMode
				? <CollapsibleFormEditResume resume={resume} isStandardResume={isStandardResume}
													  name={name} setName={setName}
													  addressing={addressing} setAddressing={setAddressing}
													  userIds={userIds} setUserIds={setUserIds}
													  usersSelectOptions={usersSelectOptions}/>
				: <ViewResumePage resume={preview}/>}
		</>
	)
}

export default EditResumePage
