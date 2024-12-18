import { Button } from '../../components'
import { ChevronLeftOutline as Left, ChevronRightOutline as Right } from '@graywolfai/react-heroicons'

export function CommentList({ comments, variant, nextPage, prevPage, hasNext, hasPrevious }) {

	if (variant === 'user') {
		return <CommentListUser comments = { comments }/>
	} else {
		return <CommentListMovie comments = { comments } nextPage = { nextPage } prevPage = { prevPage }  hasNext = { hasNext }  hasPrevious = { hasPrevious }/>
	}

}

function CommentListUser({ comments }) {

	if (comments.content.length === 0) {
		return <>
			<span className = 'block p-8 text-center bg-gray-300 font-bold'>
				No has comentado aún!
			</span>
		</>
	} else {
		return <>
			<div className = 'w-full flex items-center gap-4'>
				<ul className = 'w-full flex-1 gap-2 relative items-center justify-center'>
					{ comments.content.map(comment =>
						<li key = { comment.id } className = 'bg-white p-4 rounded shadow mb-5'>
							<div className = 'flex items-center justify-between w-full'>
								<div className = 'flex items-center gap-4'>
									<span className = 'font-bold'>{ comment.movie.title }</span>
									<Button className='mt-auto' variant="primary" onClick = { () => window.location.href = `/movies/${ comment.movie.id }` }>
										Ver película
									</Button>
								</div>
								<span className = 'font-bold'>{ comment.rating } / 10</span>
							</div>
							<hr className = 'my-4'/>
							{ comment.comment }
						</li>
					)}
				</ul>
			</div>
		</>
	}

}

function CommentListMovie({ comments, nextPage, prevPage, hasNext, hasPrevious }) {

	if (comments.content.length === 0) {
		return <>
			<span className = 'block p-8 text-center bg-gray-300 font-bold'>
				Nadie ha comentado aún!
			</span>
		</>
	} else {
		return <>
			<div className = 'w-full flex items-center gap-4'>
				<Button className = 'rounded-full' variant = 'primary' disabled = { !hasPrevious } onClick = { prevPage }>
					<Left className = 'w-6 h-6 pointer-events-none'/>
				</Button>
				<ul className = 'w-full flex-1 grid grid-cols-3 gap-2 relative items-center justify-center'>
					{ comments.content.map(comment =>
						<li key = { comment.id } className = 'bg-white p-4 rounded shadow'>
							<div className = 'flex items-center justify-between w-full'>
								<span className = 'font-bold'>{ comment.user.email }</span>
								<span className = 'font-bold'>{ comment.rating } / 10</span>
							</div>
							<hr className = 'my-4'/>
							{ comment.comment }
						</li>
					)}
				</ul>
				<Button className = 'rounded-full' variant = 'primary' disabled = { !hasNext } onClick = { nextPage }>
					<Right className = 'w-6 h-6 pointer-events-none'/>
				</Button>
			</div>
		</>
	}

}
