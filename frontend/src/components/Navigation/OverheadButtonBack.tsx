import React from "react";
import {useNavigate} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBackward} from "@fortawesome/free-solid-svg-icons";

function OverheadButtonBack(props: { left?: boolean }) {
	const navigate = useNavigate()

	return (
		<aside className={"overhead " + (props.left ? " left" : " right")}>
			<button className="overhead--button" onClick={() => navigate(-1)}>
				<FontAwesomeIcon icon={faBackward} size="2xl"/>
			</button>
		</aside>
	)
}

export default OverheadButtonBack
