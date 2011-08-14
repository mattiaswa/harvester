package harvester;

public class RelatedKeyword implements Comparable<RelatedKeyword> {
	private final String word;
	private final int relevance;

	public RelatedKeyword(String word, int relevance) {
		this.word = word;
		this.relevance = relevance;
	}

	@Override
	public String toString() {
		return "RelatedKeyword [word=" + word + ", relevance=" + relevance
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + relevance;
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelatedKeyword other = (RelatedKeyword) obj;
		if (relevance != other.relevance)
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}

	public String getWord() {
	    return word;
	}

	public int getRelevance() {
	    return relevance;
	}

	@Override
	public int compareTo(RelatedKeyword o) {
		return o.relevance - this.relevance;
	}
}
