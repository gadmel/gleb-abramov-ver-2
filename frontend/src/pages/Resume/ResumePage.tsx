import React from 'react';
import LoadingScreen from "../../components/LoadingScreen/LoadingScreen";
import ViewResumePage from "../../components/Resume/ViewResumePage";
import EditResumePage from "./EditResumePage";
import useResume from "../../hooks/useResume";

function ResumePage() {
	const {resume, isAdminView, editMode, setEditMode} = useResume()

	return isAdminView
		? <EditResumePage resume={resume} editMode={editMode} setEditMode={setEditMode}/>
		: !resume
			? <LoadingScreen/>
			: <ViewResumePage resume={resume}/>
}

export default ResumePage;
