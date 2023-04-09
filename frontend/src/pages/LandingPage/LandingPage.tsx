import React, {useRef} from "react";

import Layout from "../../components/Layout/Layout";
import glebAbramov from "../../assets/gleb_photo_squared.jpg";

function LandingPage() {

	const summaryRef = useRef<HTMLDivElement>(null)
	const personalityRef = useRef<HTMLDivElement>(null)
	const codingRef = useRef<HTMLDivElement>(null)

	const handleScrollIntoView = (ref: React.RefObject<HTMLDivElement>) => {
		ref.current?.scrollIntoView({behavior: 'smooth'})
	}

	return (
		<Layout title="Fullstack Development">
			<section id="summary" ref={summaryRef}>
				<div className="full-screen-unit">
					<div className="avatar--wrapper">
						<img src={glebAbramov} alt="Gleb Abramov" className="avatar"/>
					</div>
					<p className="greetings">Hello, my name is</p>
					<p className="name">Gleb Abramov</p>
					<p className="job-title">I am a Fullstack Developer from Hamburg, Germany.</p>
					<button className="button--welcome" onClick={() => handleScrollIntoView(personalityRef)}>
						Welcome!
					</button>
				</div>
			</section>

			<section id="details" ref={personalityRef}>
				<div className="full-screen-unit">

					<p className="statement">I love listening to recorded music, listening to live music, playing live
						music, pretending to play live music and to produce sounds associated with music.</p>
					<p className="extension">Mostly using string instruments such as bass-guitar, guitar,
						bass-balalaika
						(I wish I had one), drums (I wish I had one), and almost any even surface that can be used as a
						drum.</p>
					<p className="conclusion">And I am very passionate about it.</p>

					<p className="extension">My beloved spouse posses a very beautiful-sounding piano, which she
						gratefully allows me to play. So I produce these sounds also, they can hardly be called music
						though;</p>
					<p className="conclusion">She does it obviously because she loves me.</p>
					<button className="button--go-on" onClick={() => handleScrollIntoView(codingRef)}>
						Go on
					</button>
				</div>
			</section>

			<section id="details2" ref={codingRef}>
				<div className="full-screen-unit">
					<p className="statement">To produce I also love a clean and maintainable code with wide yet
						rational test coverage.</p>
					<p className="conclusion">And I am very passionate about it too.</p>
					<p className="extension">Professionally I normally used to work with <span className="stack">React, TypeScript,
							JavaScript(ES6+), CSS3, SASS, MongoDB, MySQL, Jira, Git,</span> provide some shallow knowledge
						of <span className="stack">Google
							Cloud Platform, Firebase</span> and recently I have been deeply instructed in <span
							className="stack">Java, Maven, Spring-Boot, Docker</span> (see neufische's Bootcamp to make a
						full picture). <br/>I really enjoy working with these
						technologies.</p>
					<p className="conclusion">I am a big fan of the DRY principle and I am always looking for ways to
						improve my code.</p>
					<p className="extension">In my free time I used to explore new technologies and frameworks and have
						been fallen in love with <span className="stack">Flutter</span> and <span
							className="stack">Dart</span>. Pity I have not had enough time to dive deeper into it but I
						am
						very excited about it.</p>

					<button className="button--scroll-up" onClick={() => handleScrollIntoView(summaryRef)}>
						Scroll up
					</button>

				</div>
			</section>
		</Layout>
	)
}

export default LandingPage;
