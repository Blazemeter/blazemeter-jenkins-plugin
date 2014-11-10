package hudson.plugins.blazemeter.entities;

/**
 * User: Vitali
 * Date: 4/2/12
 * Time: 14:05
 * <p/>
 * Updated - Platform independent
 * User: moshe
 * Date: 5/12/12
 * Time: 1:05 PM
 */
public class TestInfo {
    private String id;
    private String name;
    private String status;

    public TestInfo() {
    }

    public TestInfo(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestInfo testInfo = (TestInfo) o;

        if (id != null ? !id.equals(testInfo.id) : testInfo.id != null) return false;
        if (name != null ? !name.equals(testInfo.name) : testInfo.name != null) return false;
        if (status != null ? !status.equals(testInfo.status) : testInfo.status != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TestInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }


}
