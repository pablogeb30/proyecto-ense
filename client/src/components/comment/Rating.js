import { useState } from "react";

export function Rating({ onRatingSelected }) {

	const [rating, setRating] = useState(0); // Valor seleccionado
	const [hoveredRating, setHoveredRating] = useState(0); // Valor cuando pasas el mouse

	// Manejar la selección de puntuación
	const handleRating = (value) => {
		setRating(value);
		if (onRatingSelected) {
			onRatingSelected(value);
		}
	};

	// Manejar hover
	const handleMouseEnter = (value) => setHoveredRating(value);
	const handleMouseLeave = () => setHoveredRating(0);

	return (
		<div className="flex items-center gap-2">
			{[...Array(10)].map((_, index) => {
				const value = index + 1;
				return (
					<button
						type="button"
						key={value}
						onClick={() => handleRating(value)}
						onMouseEnter={() => handleMouseEnter(value)}
						onMouseLeave={handleMouseLeave}
						className={`w-6 h-6 rounded-full border-2 ${
							value <= (hoveredRating || rating)
								? "bg-yellow-500 border-yellow-500 scale-110"
								: "bg-gray-300 border-gray-400 hover:bg-yellow-400"
						} transform transition duration-200`}
						aria-label={`Rate ${value}`}
					></button>
				);
			})}
		</div>
	);
}
