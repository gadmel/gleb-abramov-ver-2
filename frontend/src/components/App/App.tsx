import React from 'react'
import {Route, Routes} from 'react-router-dom'

import LandingPage from "../LandingPage/LandingPage";
import LegalNotice from "../LegalNotice/LegalNotice";

function App() {

	return (
		<div className="App">
			<Routes>
				<Route path="/" element={<LandingPage/>}/>
				<Route path="/legal" element={<LegalNotice/>}/>
			</Routes>
		</div>
	)
}

export default App
