package com.kingghost.pms.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "tcma_info")
/**
 * tcma is short of thread class method and args
 * 
 * @author kingghost
 *
 */
public class TCMAInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	@Column(name = "parent_id")
	private Long parentId;
	@Column(name = "thread_name")
	private String threadName;
	@Column(name = "class_name")
	private String className;
	@Column(name = "method_name")
	private String methodName;
	@Column(name = "args")
	private String args;
	@Column(name = "use_time")
	private Long useTime;
	@Column(name = "total_time")
	private Long totalTime;
	@Column(name = "use_ratio")
	private Double useRatio;
	private String flag;
	@Column(name = "order_no")
	private Integer orderNo;
	@OneToMany(targetEntity = TCMAInfo.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_id")
	@OrderBy("order_no")
	private List<TCMAInfo> children;

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}

	public List<TCMAInfo> getChildren() {
		return children;
	}

	public void setChildren(List<TCMAInfo> children) {
		this.children = children;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public Long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(Long useTime) {
		this.totalTime = useTime;
	}

	public Double getUseRatio() {
		return useRatio;
	}

	public void setUseRatio(Double useRatio) {
		this.useRatio = useRatio;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Long getUseTime() {
		return useTime;
	}

	public void setUseTime(Long useTime) {
		this.useTime = useTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + ((threadName == null) ? 0 : threadName.hashCode());
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
		TCMAInfo other = (TCMAInfo) obj;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (threadName == null) {
			if (other.threadName != null)
				return false;
		} else if (!threadName.equals(other.threadName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TCMAInfo [id=" + id + ", parentId=" + parentId + ", threadName=" + threadName + ", className=" + className + ", methodName=" + methodName + ", args=" + args + ", useTime=" + useTime + ", totalTime=" + totalTime
				+ ", useRatio=" + useRatio + ", flag=" + flag + ", orderNo=" + orderNo + ", children=" + children + "]";
	}

}
