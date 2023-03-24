import React from 'react';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faSpinner} from "@fortawesome/free-solid-svg-icons";

function LoadingScreen() {
  return (
	  <section id="loading">
		  <div className="full-screen-unit">
			  <FontAwesomeIcon icon={faSpinner} size="2xl" className="loading-spinner fa-spin-pulse" />
		  </div>
	  </section>
  );
}

export default LoadingScreen;
