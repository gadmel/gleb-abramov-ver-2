import React from "react";
import {useNavigate} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faHouse} from "@fortawesome/free-solid-svg-icons";

function OverheadButtonHome(props: { right?: boolean }) {
	const navigate = useNavigate()

	return (
		<aside className={"overhead " + (props.right ? " right" : " left")}>
			<button className="overhead--button" onClick={() => navigate("/")}>
				<FontAwesomeIcon icon={faHouse} size="2xl"/>
			</button>
		</aside>
	)
}

export default OverheadButtonHome
