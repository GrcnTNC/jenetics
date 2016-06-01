/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 */
package org.jenetix;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.jenetics.internal.util.require;

import org.jenetics.Chromosome;
import org.jenetics.util.ISeq;

import org.jenetix.util.TreeNode;

/**
 * Static helper methods concerning {@code TreeGene}s.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
final class TreeGenes {
	private TreeGenes() {require.noInstance();}

	/**
	 * Return a {@link TreeNode} with {@code this} tree-gene as root.
	 *
	 * @param gene the root tree-gene
	 * @param chromosome the chromosome which {@code this} tree-gene is part of
	 * @return a {@link TreeNode} with {@code this} tree-gene as root
	 * @throws NullPointerException if one of the given arguments is {@code null}
	 */
	public static <A, G extends TreeGene<A, G>> TreeNode<A>
	toTreeNode(final G gene, final Chromosome<? extends G> chromosome) {
		requireNonNull(chromosome);

		final TreeNode<A> root = TreeNode.of();
		TreeGenes.<A, G>fill(gene, root, chromosome);
		return root;
	}

	private static <A, G extends TreeGene<A, G>> void fill(
		final G gene,
		final TreeNode<A> parent,
		final Chromosome<? extends G> chromosome
	) {
		parent.setValue(gene.getAllele());

		gene.children(chromosome).forEachOrdered(child -> {
			final TreeNode<A> node = TreeNode.of();
			parent.add(node);
			TreeGenes.<A, G>fill(child, node, chromosome);
		});
	}

	/**
	 *
	 * @param newGene
	 * @param <A>
	 * @param <G>
	 * @return
	 */
	public static <A, G extends TreeGene<A, G>>
	Collector<TreeNode<A>, ?, ISeq<G>>
	toTreeGene(final BiFunction<A, int[], G> newGene) {
		return Collector.of(
			(Supplier<List<TreeNode<A>>>)ArrayList::new,
			List::add,
			(left, right) -> { left.addAll(right); return left; },
			nodes -> {
				final Map<TreeNode<A>, Integer> indexes = new LinkedHashMap<>();
				for (int i = 0; i < nodes.size(); ++i) {
					indexes.put(nodes.get(i), i);
				}

				return nodes.stream()
					.map(node -> newGene.apply(
						node.getValue(),
						node.childStream()
							.mapToInt(indexes::get)
							.toArray()))
					.collect(ISeq.toISeq());
			}
		);
	}

}
