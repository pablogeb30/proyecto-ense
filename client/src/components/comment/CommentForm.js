import { Button, Rating } from '../../components'

export function CommentForm({ setComment, setRating, handleSubmit }) {

	return (
		<form className="flex gap-4 rounded-lg mt-8" onSubmit={handleSubmit}>
			<div className="flex-none w-1/4 flex flex-col items-center justify-between gap-4">
				<div className="w-full flex flex-col items-center gap-4">
					<label className="block text-sm font-semibold">
						Y a ti, ¿qué te ha parecido?
					</label>
					<div>
						<Rating onRatingSelected={(value) => {
							setRating(value)
						}} />
					</div>
				</div>
				<Button className="mt-auto w-full" type="submit" variant="primary">
					Publicar
				</Button>
			</div>
			<div className="flex-1">
				<textarea
					required
					maxLength="500"
					minLength="1"
					id="textarea"
					rows="8"
					className="w-full h-full p-2 border border-gray-300 rounded-lg"
					placeholder="Escribe aquí tu comentario"
					onChange={(e) => setComment(e.target.value)}
				></textarea>
			</div>
		</form>
	)

}