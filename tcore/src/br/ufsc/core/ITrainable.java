package br.ufsc.core;

import java.util.List;

public interface ITrainable<T> {

	void train(List<T> train);
}
