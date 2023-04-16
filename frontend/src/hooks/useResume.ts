import {useEffect, useState} from "react";
import {useLocation} from "react-router-dom";
import useAuth from "./useAuth";
import resumeService, {Resume} from "../services/resumeService";

function useResume() {
	const user = useAuth(true)
	const {state} = useLocation();
	const [editMode, setEditMode] = useState<boolean>(false);
	const [isAdminView, setIsAdminView] = useState<boolean>(false);
	const [resume, setResume] = useState<Resume>(state?.resume || {} as Resume);

	useEffect(() => {
		if (state?.resume) {
			setResume(state.resume)
			setIsAdminView(state.editMode)
			setEditMode(state.editMode)
		} else {
			resumeService
				.getResume()
				.then((resume: Resume) => setResume(resume))
		}
	}, [user, state])

	return {resume, setResume, isAdminView, editMode, setEditMode}

}

export default useResume
