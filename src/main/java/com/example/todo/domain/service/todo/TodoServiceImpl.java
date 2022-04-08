package com.example.todo.domain.service.todo;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.exception.ResourceNotFoundException;
import org.terasoluna.gfw.common.message.ResultMessage;
import org.terasoluna.gfw.common.message.ResultMessages;

import com.example.todo.domain.model.Todo;
import com.example.todo.domain.repository.todo.TodoRepository;


@Service
@Transactional
public class TodoServiceImpl implements TodoService {
	
	private static final long MAX_UNIFINISHED_COUNT = 5;

	@Inject
	TodoRepository todoRepository;
	
	@Override
	@Transactional(readOnly = true)
	public Collection<Todo> findAll() {
		return todoRepository.findAll();
	}

	@Override
	public Todo create(Todo todo) {
		long unfinishedCount = todoRepository.countByFinished(false);
		if (unfinishedCount >= MAX_UNIFINISHED_COUNT) {
			ResultMessages messages = ResultMessages.error();
			messages.add(ResultMessage
					.fromText("[E001] The count of un-finished Todo must not be over "
							+ MAX_UNIFINISHED_COUNT + "."));
			throw new BusinessException(messages);
		}
		
		// 重複しないランダムなIDを生成する
		String todoId = UUID.randomUUID().toString();
		Date createdAt = new Date();
		
		todo.setTodoId(todoId);
		todo.setCreatedAt(createdAt);
		todo.setFinished(false);
		
		todoRepository.create(todo);
		
		return todo;		
	}

	@Override
	public Todo finish(String todoId) {
		Todo todo = findOne(todoId);
		if (todo.isFinished()) {
			ResultMessages messages = ResultMessages.error();
			messages.add(ResultMessage
					.fromText("[E402] The requested Todo is already finished. (id="
							+ todoId + ")"));
			throw new BusinessException(messages);
		}
		
		todo.setFinished(true);
		todoRepository.update(todo);
		return todo;
	}
	
	@Override
	public void delete(String todoId) {
		Todo todo = findOne(todoId);
		todoRepository.delete(todo);
	}
	
	private Todo findOne(String todoId) {
		return todoRepository.findById(todoId).orElseThrow(() -> {
			ResultMessages messages = ResultMessages.error();
			messages.add(ResultMessage
					.fromText("[E404] The requested Todo is not found. (id="
							+ todoId + ")"));
			return new ResourceNotFoundException(messages);
		});
	}

}
