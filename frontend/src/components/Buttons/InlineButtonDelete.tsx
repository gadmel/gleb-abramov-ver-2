import React, {useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTrash, faTimes} from "@fortawesome/free-solid-svg-icons";

interface Props {
	id: string
	role?: string
	handleConfirmedDelete: () => void
}


const DeleteButton = (props: Props) => {

	const [sureToDelete, setSureToDelete] = useState<string | null>(null)

	return (
		<>
			{sureToDelete !== props.id
				? <button className="action-button action-button--delete" onClick={() => setSureToDelete(props.id)}>
					<FontAwesomeIcon icon={faTrash}/>
				</button>
				: <>
					<button className="action-button action-button--delete"
							  onClick={props.handleConfirmedDelete}>
						<FontAwesomeIcon icon={faTrash}/>
					</button>
					<button className="action-button action-button--cancel" onClick={() => setSureToDelete(null)}>
						<FontAwesomeIcon icon={faTimes}/>
					</button>
				</>
			}
		</>
	)
}

export default DeleteButton
