/**
 * Copyright 2009 Wilfred Springer
 * Copyright 2012 Jason Pell
 * Copyright 2013 Antonio García-Domínguez
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * The class is bundled together with our code because it has not been
 * released to any central repository.
 */
package repackaged.nl.flotsam.xeger;

import java.util.List;
import java.util.Random;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * An object that will generate text from a regular expression. In a way, it's the opposite of a regular expression
 * matcher: an instance of this class will produce text that is guaranteed to match the regular expression passed in.
 *
 * slight modifications by Marcin Grzejszczak
 */
public class Xeger {

	private final Automaton automaton;
	private Random random;
	// Added by Marcin Grzejszczak
	// may lead to stackoverflow when regex is unbounded
	static int ITERATION_LIMIT = 200;

	/**
	 * Constructs a new instance, accepting the regular expression and the randomizer.
	 *
	 * @param regex  The regular expression. (Not <code>null</code>.)
	 * @param random The object that will randomize the way the String is generated. (Not <code>null</code>.)
	 * @throws IllegalArgumentException If the regular expression is invalid.
	 */
	public Xeger(String regex, Random random) {
		assert regex != null;
		assert random != null;
		// https://stackoverflow.com/questions/1578789/how-do-i-generate-text-matching-a-regular-expression-from-a-regular-expression
		String pattern = regex
				.replace("\\d", "[0-9]")        // Used d=Digit
				.replace("\\w", "[A-Za-z0-9_]") // Used =Word
				.replace("\\s", "[ \t\r\n]");   // Used s="White"Space
		this.automaton = new RegExp(pattern).toAutomaton();
		this.random = random;
	}

	/**
	 * As {@link Xeger#Xeger(String, java.util.Random)}, creating a {@link java.util.Random} instance
	 * implicityly.
	 *
	 * @param regex as string
	 */
	public Xeger(String regex) {
		this(regex, new Random());
	}

	/**
	 * Generates a random String that is guaranteed to match the regular expression passed to the constructor.
	 * @return generated regexp
	 */
	public String generate() {
		StringBuilder builder = new StringBuilder();
		int counter = 0;
		generate(builder, this.automaton.getInitialState(), counter);
		return builder.toString();
	}

	private void generate(StringBuilder builder, State state, int counter) {
		if (counter >= ITERATION_LIMIT) {
			return;
		}
		List<Transition> transitions = state.getSortedTransitions(false);
		if (transitions.size() == 0) {
			assert state.isAccept();
			return;
		}
		int nroptions = state.isAccept() ? transitions.size() : transitions.size() - 1;
		int option = Xeger.getRandomInt(0, nroptions, this.random);
		if (state.isAccept() && option == 0) {          // 0 is considered stop
			return;
		}
		// Moving on to next transition
		Transition transition = transitions.get(option - (state.isAccept() ? 1 : 0));
		appendChoice(builder, transition);
		generate(builder, transition.getDest(), ++counter);
	}

	private void appendChoice(StringBuilder builder, Transition transition) {
		char c = (char) Xeger
				.getRandomInt(transition.getMin(), transition.getMax(), this.random);
		builder.append(c);
	}

	public Random getRandom() {
		return this.random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	/**
	 * Generates a random number within the given bounds.
	 *
	 * @param min The minimum number (inclusive).
	 * @param max The maximum number (inclusive).
	 * @param random The object used as the randomizer.
	 * @return A random number in the given range.
	 */
	static int getRandomInt(int min, int max, Random random) {
		// Use random.nextInt as it guarantees a uniform distribution
		int maxForRandom = max - min + 1;
		return random.nextInt(maxForRandom) + min;
	}
}