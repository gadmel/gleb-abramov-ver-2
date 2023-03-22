import React from "react";
import {useNavigate} from "react-router-dom";
import Layout from "../Layout/Layout";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faXmark} from "@fortawesome/free-solid-svg-icons";

function LegalNotice() {

	const navigate = useNavigate();
	const TMG_section_5 = "https://www.gesetze-im-internet.de/tmg/__5.html"

	return (
		<Layout title="Legal Notice">
			<aside className="overhead">
				<button className="overhead--button" onClick={() => navigate("/")}>
					<FontAwesomeIcon icon={faXmark} size="2xl" />
				</button>
			</aside>

			<section id="legal-notice">
				<div className="full-screen-unit">

					<div className="block">
						<h1>Legal Notice</h1>
						<p className="title">Information in accordance with <a href={TMG_section_5}>Section 5 TMG</a></p>
						<p className="value">
							Gleb Abramov<br/>
							Försterweg 167<br/>
							22525 Hamburg
						</p>
					</div>

					<div className="block">
						<h3>Contact Information</h3>
						<p className="value">
							E-Mail: <a href="mailto:admin@gleb-abramov.com">admin@gleb-abramov.com</a>
						</p>
					</div>

					<div className="block">
						<h2>Disclaimer</h2>
						<h3>Accountability for content</h3>
						<p className="notice">
							The contents of my pages have been created with the utmost care. However, I cannot guarantee the
							contents' accuracy, completeness or topicality. According to statutory provisions, I am furthermore
							responsible for my own content on these web pages. In this matter, please note that I am not
							obliged
							to monitor the transmitted or saved information of third parties, or investigate circumstances
							pointing to illegal activity. My obligations to remove or block the use of information under
							generally
							applicable laws remain unaffected by this as per &sect;&sect; 8 to 10 of the Telemedia Act (TMG).
						</p>
					</div>

				</div>
			</section>
		</Layout>
	)
}

export default LegalNotice;