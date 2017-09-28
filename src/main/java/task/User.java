package task;

public class User {

	private String name;

	private String address;

	public void setName(String name) {
		this.name = name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public String toString() {
		return String.format("User{ name : %s ,address : %s}", getName(), getAddress());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		User user = (User) o;

		if (name != null ? !name.equals(user.name) : user.name != null)
			return false;

		return address != null ? address.equals(user.address) : user.address == null;
	}

}
