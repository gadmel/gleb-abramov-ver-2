import {Resume} from "../../services/resumeService";
import Layout from "../Layout/Layout";
import OverheadButtonHome from "../Navigation/OverheadButtonHome";
import OverheadButtonBack from "../Navigation/OverheadButtonBack";

type Props = {
	resume: Resume
}

function ViewResumePage(props: Props) {
	return (
		<Layout title={"Resume for " + props.resume?.name + " by Gleb Abramov"}>
			<OverheadButtonHome/>
			<OverheadButtonBack/>
			<section id="resume">
				<div className="full-screen-unit">
					<h1>Resume Page</h1>
					<p>{props.resume?.name}</p>
				</div>
			</section>
		</Layout>
	)
}

export default ViewResumePage
