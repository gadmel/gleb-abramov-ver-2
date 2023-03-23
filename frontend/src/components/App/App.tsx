import React from 'react'
import {Route, Routes} from 'react-router-dom'

import LandingPage from "../LandingPage/LandingPage";
import LegalNotice from "../LegalNotice/LegalNotice";
import Login from "../Login/Login";
import Register from "../Register/Register";

function App() {

	return (
		<div className="App">
			<Routes>
				<Route path="/" element={<LandingPage/>}/>
				<Route path="/legal" element={<LegalNotice/>}/>

				<Route path={"/login"} element={<Login/>} />
				<Route path={"/register"} element={<Register/>} />
				<Route path="*" element={<h1>404 - Page not found</h1>}/>

			</Routes>
		</div>
	)
}

export default App
